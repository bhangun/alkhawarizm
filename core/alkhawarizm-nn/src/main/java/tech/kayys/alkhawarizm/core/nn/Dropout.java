package tech.kayys.alkhawarizm.core.nn;

import tech.kayys.alkhawarizm.core.tensor.Tensor;

/**
 * Dropout layer for regularization during training.
 */
public class Dropout extends NNModule {

    private final float probability;

    public Dropout(float p) {
        this.probability = p;
    }

    public Dropout() {
        this(0.5f);
    }

    @Override
    public Tensor forward(Tensor input) {
        return input.dropout(probability, training);
    }

    public float probability() {
        return probability;
    }

    @Override
    public String toString() {
        return String.format("Dropout(p=%.2f)", probability);
    }
}
