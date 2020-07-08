package io.harness.ccm.billing;

import static io.harness.rule.OwnerRule.HANTANG;
import static io.harness.rule.OwnerRule.ROHIT;
import static org.assertj.core.api.Assertions.assertThat;

import io.harness.CategoryTest;
import io.harness.category.element.UnitTests;
import io.harness.ccm.billing.bigquery.TruncExpression;
import io.harness.ccm.billing.graphql.CloudBillingGroupBy;
import io.harness.ccm.billing.graphql.CloudEntityGroupBy;
import io.harness.ccm.billing.graphql.TimeTruncGroupby;
import io.harness.rule.Owner;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class CloudBillingGroupByTest extends CategoryTest {
  private CloudBillingGroupBy cloudBillingGroupBy;
  private CloudBillingGroupBy cloudBillingEntityGroupBy;

  @Before
  public void setUp() {
    cloudBillingGroupBy = new CloudBillingGroupBy();
    cloudBillingEntityGroupBy = new CloudBillingGroupBy();
    cloudBillingGroupBy.setTimeTruncGroupby(
        TimeTruncGroupby.builder().resolution(TruncExpression.DatePart.DAY).build());
  }

  @Test
  @Owner(developers = HANTANG)
  @Category(UnitTests.class)
  public void testToGroupbyObject() {
    Object groupbyObject = cloudBillingGroupBy.toGroupbyObject();
    assertThat(groupbyObject.toString()).isEqualTo("TIMESTAMP_TRUNC(t0.startTime,DAY) AS start_time_trunc");
  }

  @Test
  @Owner(developers = ROHIT)
  @Category(UnitTests.class)
  public void testRawTableTimeToGroupbyObject() {
    Object groupbyObject = cloudBillingGroupBy.toRawTableGroupbyObject();
    assertThat(groupbyObject.toString()).isEqualTo("TIMESTAMP_TRUNC(t0.usage_start_time,DAY) AS start_time_trunc");
    cloudBillingEntityGroupBy.setEntityGroupBy(CloudEntityGroupBy.projectId);
    groupbyObject = cloudBillingEntityGroupBy.toRawTableGroupbyObject();
    assertThat(groupbyObject).isEqualTo(RawBillingTableSchema.gcpProjectId);

    cloudBillingEntityGroupBy.setEntityGroupBy(CloudEntityGroupBy.product);
    groupbyObject = cloudBillingEntityGroupBy.toRawTableGroupbyObject();
    assertThat(groupbyObject).isEqualTo(RawBillingTableSchema.gcpProduct);

    cloudBillingEntityGroupBy.setEntityGroupBy(CloudEntityGroupBy.billingAccountId);
    groupbyObject = cloudBillingEntityGroupBy.toRawTableGroupbyObject();
    assertThat(groupbyObject).isEqualTo(RawBillingTableSchema.gcpBillingAccountId);

    cloudBillingEntityGroupBy.setEntityGroupBy(CloudEntityGroupBy.skuId);
    groupbyObject = cloudBillingEntityGroupBy.toRawTableGroupbyObject();
    assertThat(groupbyObject).isEqualTo(RawBillingTableSchema.gcpSkuId);

    cloudBillingEntityGroupBy.setEntityGroupBy(CloudEntityGroupBy.sku);
    groupbyObject = cloudBillingEntityGroupBy.toRawTableGroupbyObject();
    assertThat(groupbyObject).isEqualTo(RawBillingTableSchema.gcpSkuDescription);

    cloudBillingEntityGroupBy.setEntityGroupBy(CloudEntityGroupBy.region);
    groupbyObject = cloudBillingEntityGroupBy.toRawTableGroupbyObject();
    assertThat(groupbyObject).isEqualTo(RawBillingTableSchema.region);

    cloudBillingEntityGroupBy.setEntityGroupBy(CloudEntityGroupBy.labelsKey);
    groupbyObject = cloudBillingEntityGroupBy.toRawTableGroupbyObject();
    assertThat(groupbyObject).isEqualTo(RawBillingTableSchema.labelsKey);

    cloudBillingEntityGroupBy.setEntityGroupBy(CloudEntityGroupBy.labelsValue);
    groupbyObject = cloudBillingEntityGroupBy.toRawTableGroupbyObject();
    assertThat(groupbyObject).isEqualTo(RawBillingTableSchema.labelsValue);
  }
}
