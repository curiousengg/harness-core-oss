package io.harness.ci.execution.dao;

import io.harness.annotation.HarnessRepo;
import io.harness.ci.beans.entities.CIBuild;

import org.springframework.data.repository.PagingAndSortingRepository;

@HarnessRepo
public interface CIBuildRepository extends PagingAndSortingRepository<CIBuild, String> {}
