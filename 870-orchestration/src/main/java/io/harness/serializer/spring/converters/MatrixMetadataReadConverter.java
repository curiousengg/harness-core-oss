package io.harness.serializer.spring.converters;

import static io.harness.annotations.dev.HarnessTeam.CDC;

import io.harness.annotations.dev.OwnedBy;
import io.harness.pms.contracts.execution.MatrixMetadata;
import io.harness.serializer.spring.ProtoReadConverter;

import com.google.inject.Singleton;
import org.springframework.data.convert.ReadingConverter;

@OwnedBy(CDC)
@Singleton
@ReadingConverter
public class MatrixMetadataReadConverter extends ProtoReadConverter<MatrixMetadata> {
  public MatrixMetadataReadConverter() {
    super(MatrixMetadata.class);
  }
}
