package io.cdap.directives.parser;
import io.cdap.wrangler.api.parser.Token;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.parser.GrammarBasedParser;
import io.cdap.wrangler.registry.DirectiveRegistry;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class GrammarBasedParserTest {

    @Test
    public void testParseByteSizeAndTimeDuration() throws Exception {
        DirectiveRegistry registry = (DirectiveRegistry) new SimpleDirectiveRegistry();

        GrammarBasedParser parser = new GrammarBasedParser("default", "", registry);

        // ByteSize
        String directive1 = "set-column :col 10KB";
        List<Token> tokens1 = parser.parseTokens(directive1);

        Assert.assertEquals(3, tokens1.size());
        Assert.assertEquals(TokenType.DIRECTIVE_NAME, tokens1.get(0).type());
        Assert.assertEquals("set-column", tokens1.get(0).value());
        Assert.assertEquals(TokenType.COLUMN_NAME, tokens1.get(1).type());
        Assert.assertEquals("col", tokens1.get(1).value());
        Assert.assertEquals(TokenType.BYTE_SIZE, tokens1.get(2).type());
        Assert.assertEquals("10KB", tokens1.get(2).value());

        // TimeDuration
        String directive2 = "set-column :col 500ms";
        List<Token> tokens2 = parser.parseTokens(directive2);

        Assert.assertEquals(3, tokens2.size());
        Assert.assertEquals(TokenType.DIRECTIVE_NAME, tokens2.get(0).type());
        Assert.assertEquals("set-column", tokens2.get(0).value());
        Assert.assertEquals(TokenType.COLUMN_NAME, tokens2.get(1).type());
        Assert.assertEquals("col", tokens2.get(1).value());
        Assert.assertEquals(TokenType.TIME_DURATION, tokens2.get(2).type());
        Assert.assertEquals("500ms", tokens2.get(2).value());

        // aggregate-stats
        String directive3 = "aggregate-stats :size :time total_size total_time MB s";
        List<Token> tokens3 = parser.parseTokens(directive3);

        Assert.assertEquals(7, tokens3.size());
        Assert.assertEquals(TokenType.DIRECTIVE_NAME, tokens3.get(0).type());
        Assert.assertEquals("aggregate-stats", tokens3.get(0).value());
        Assert.assertEquals("MB", tokens3.get(5).value());
        Assert.assertEquals("s", tokens3.get(6).value());
    }
}
