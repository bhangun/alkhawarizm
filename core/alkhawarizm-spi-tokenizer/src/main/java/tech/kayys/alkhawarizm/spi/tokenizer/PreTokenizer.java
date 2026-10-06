package tech.kayys.alkhawarizm.spi.tokenizer;

import java.util.List;

public interface PreTokenizer {
    List<String> split(String text);
}