package com.mycompany.app.discord.formatter;

import org.junit.jupiter.api.Test;

import com.mycompany.app.model.Competition;

import java.util.List;
import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;
public class ErrorFormatterTest {
    
    @Test
    void shouldFormatErrorsCorrectly() {
        List<Competition> comps = Collections.emptyList();
        String result = ErrorFormatter.format(comps,"We will ask the questions");
        assertThat(result)
        .contains("We will ask the questions")
        .contains("Available competitions") // we check whether it call CompFormatter correctly
        .doesNotContain("null");
    }
}
