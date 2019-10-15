package software.wings.service.impl;

import com.google.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.harness.persistence.PersistentEntity;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.hibernate.validator.constraints.NotBlank;
import software.wings.beans.EntityType;
import software.wings.search.entities.application.ApplicationSearchEntity;
import software.wings.search.entities.application.ApplicationSearchResult;
import software.wings.search.entities.application.ApplicationView;
import software.wings.search.entities.deployment.DeploymentSearchEntity;
import software.wings.search.entities.deployment.DeploymentSearchResult;
import software.wings.search.entities.deployment.DeploymentView;
import software.wings.search.entities.deployment.DeploymentView.DeploymentViewKeys;
import software.wings.search.entities.environment.EnvironmentSearchEntity;
import software.wings.search.entities.environment.EnvironmentSearchResult;
import software.wings.search.entities.environment.EnvironmentView;
import software.wings.search.entities.pipeline.PipelineSearchEntity;
import software.wings.search.entities.pipeline.PipelineSearchResult;
import software.wings.search.entities.pipeline.PipelineView;
import software.wings.search.entities.service.ServiceSearchEntity;
import software.wings.search.entities.service.ServiceSearchResult;
import software.wings.search.entities.service.ServiceView;
import software.wings.search.entities.workflow.WorkflowSearchEntity;
import software.wings.search.entities.workflow.WorkflowSearchResult;
import software.wings.search.entities.workflow.WorkflowView;
import software.wings.search.framework.ElasticsearchIndexManager;
import software.wings.search.framework.EntityBaseView.EntityBaseViewKeys;
import software.wings.search.framework.SearchEntity;
import software.wings.search.framework.SearchResult;
import software.wings.search.framework.SearchResults;
import software.wings.service.intfc.SearchService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ElasticsearchServiceImpl implements SearchService {
  @Inject private RestHighLevelClient client;
  @Inject private ElasticsearchIndexManager elasticsearchIndexManager;
  @Inject protected Map<Class<? extends PersistentEntity>, SearchEntity<?>> searchEntityMap;
  private static final int MAX_RESULTS = 50;
  private static final int BOOST_VALUE = 5;

  public SearchResults getSearchResults(@NotBlank String searchString, @NotBlank String accountId) throws IOException {
    SearchHits hits = search(searchString, accountId);
    LinkedHashMap<String, List<SearchResult>> searchResult = new LinkedHashMap<>();

    List<SearchResult> applicationResponseViewList = new ArrayList<>();
    List<SearchResult> pipelineResponseViewList = new ArrayList<>();
    List<SearchResult> workflowResponseViewList = new ArrayList<>();
    List<SearchResult> serviceResponseViewList = new ArrayList<>();
    List<SearchResult> environmentResponseViewList = new ArrayList<>();
    List<SearchResult> deploymentResponseViewList = new ArrayList<>();
    ObjectMapper mapper = new ObjectMapper();

    for (SearchHit hit : hits) {
      Map<String, Object> result = hit.getSourceAsMap();
      switch (EntityType.valueOf(result.get(EntityBaseViewKeys.type).toString())) {
        case APPLICATION:
          ApplicationView applicationView = mapper.convertValue(result, ApplicationView.class);
          ApplicationSearchResult applicationSearchResult = new ApplicationSearchResult(applicationView);
          applicationResponseViewList.add(applicationSearchResult);
          break;
        case SERVICE:
          ServiceView serviceView = mapper.convertValue(result, ServiceView.class);
          ServiceSearchResult serviceSearchResult = new ServiceSearchResult(serviceView);
          serviceResponseViewList.add(serviceSearchResult);
          break;
        case ENVIRONMENT:
          EnvironmentView environmentView = mapper.convertValue(result, EnvironmentView.class);
          EnvironmentSearchResult environmentSearchResult = new EnvironmentSearchResult(environmentView);
          environmentResponseViewList.add(environmentSearchResult);
          break;
        case WORKFLOW:
          WorkflowView workflowView = mapper.convertValue(result, WorkflowView.class);
          WorkflowSearchResult workflowSearchResult = new WorkflowSearchResult(workflowView);
          workflowResponseViewList.add(workflowSearchResult);
          break;
        case PIPELINE:
          PipelineView pipelineView = mapper.convertValue(result, PipelineView.class);
          PipelineSearchResult pipelineSearchResult = new PipelineSearchResult(pipelineView);
          pipelineResponseViewList.add(pipelineSearchResult);
          break;
        case DEPLOYMENT:
          if (result.get(DeploymentViewKeys.workflowInPipeline) != null
              && result.get(DeploymentViewKeys.workflowInPipeline).equals(false)) {
            DeploymentView deploymentView = mapper.convertValue(result, DeploymentView.class);
            DeploymentSearchResult deploymentSearchResult = new DeploymentSearchResult(deploymentView);
            deploymentResponseViewList.add(deploymentSearchResult);
          }
          break;
        default:
      }
    }
    searchResult.put(ApplicationSearchEntity.TYPE, applicationResponseViewList);
    searchResult.put(ServiceSearchEntity.TYPE, serviceResponseViewList);
    searchResult.put(EnvironmentSearchEntity.TYPE, environmentResponseViewList);
    searchResult.put(WorkflowSearchEntity.TYPE, workflowResponseViewList);
    searchResult.put(PipelineSearchEntity.TYPE, pipelineResponseViewList);
    searchResult.put(DeploymentSearchEntity.TYPE, deploymentResponseViewList);

    return new SearchResults(searchResult);
  }

  private SearchHits search(@NotBlank String searchString, @NotBlank String accountId) throws IOException {
    String[] indexNames = getIndexesToSearch();
    SearchRequest searchRequest = new SearchRequest(indexNames);
    BoolQueryBuilder boolQueryBuilder = createQuery(searchString, accountId);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(boolQueryBuilder).size(MAX_RESULTS);
    searchRequest.source(searchSourceBuilder);
    SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
    logger.info("Search results, time taken : {}, number of hits: {}, accountID: {}", searchResponse.getTook(),
        searchResponse.getHits().getTotalHits(), accountId);
    return searchResponse.getHits();
  }

  private String[] getIndexesToSearch() {
    return searchEntityMap.values()
        .stream()
        .map(searchEntity -> elasticsearchIndexManager.getIndexName(searchEntity.getType()))
        .toArray(String[] ::new);
  }

  private static BoolQueryBuilder createQuery(@NotBlank String searchString, @NotBlank String accountId) {
    BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
    QueryBuilder queryBuilder =
        QueryBuilders.disMaxQuery()
            .add(QueryBuilders.matchPhrasePrefixQuery(EntityBaseViewKeys.name, searchString).boost(BOOST_VALUE))
            .add(QueryBuilders.matchPhraseQuery(EntityBaseViewKeys.description, searchString))
            .tieBreaker(0.7f);
    boolQueryBuilder.must(queryBuilder).filter(QueryBuilders.termQuery(EntityBaseViewKeys.accountId, accountId));
    return boolQueryBuilder;
  }
}
