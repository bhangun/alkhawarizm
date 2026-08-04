package tech.kayys.alkhawarizm.core.tensor;

import java.nio.file.Path;

public interface ModelWeightLoader {
    boolean supports(Path path);

    WeightAdapter load(Path path);
}
