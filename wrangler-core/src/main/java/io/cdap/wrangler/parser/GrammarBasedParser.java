// (license header remains unchanged)

package io.cdap.wrangler.parser;

import com.google.common.base.Joiner;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveContext;
import io.cdap.wrangler.api.DirectiveLoadException;
import io.cdap.wrangler.api.DirectiveNotFoundException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.RecipeException;
import io.cdap.wrangler.api.RecipeParser;
import io.cdap.wrangler.api.parser.UsageDefinition;
import io.cdap.wrangler.registry.DirectiveInfo;
import io.cdap.wrangler.registry.DirectiveRegistry;
import io.cdap.wrangler.expression.EL;
import io.cdap.wrangler.expression.ELContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

// Add imports for new token classes
import io.cdap.wrangler.api.parser.Token;
import io.cdap.wrangler.api.TokenGroup;
import io.cdap.wrangler.parser.MapArguments;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TokenType;

/**
 * This class <code>GrammarBasedParser</code> is an implementation of <code>RecipeParser</code>.
 * It's responsible for compiling the recipe and checking all the directives exist before concluding
 * that the directives are ready for execution.
 */
public class GrammarBasedParser implements RecipeParser {
  private static final char EOL = '\n';
  private final String namespace;
  private final DirectiveRegistry registry;
  private final String recipe;
  private final DirectiveContext context;

  public GrammarBasedParser(String namespace, String recipe, DirectiveRegistry registry) {
    this(namespace, recipe, registry, new NoOpDirectiveContext());
  }

  public GrammarBasedParser(String namespace, String[] directives,
                            DirectiveRegistry registry, DirectiveContext context) {
    this(namespace, Joiner.on(EOL).join(directives), registry, context);
  }

  public GrammarBasedParser(String namespace, String recipe, DirectiveRegistry registry, DirectiveContext context) {
    this.namespace = namespace;
    this.recipe = recipe;
    this.registry = registry;
    this.context = context;
  }

  /**
   * Parses the recipe provided to this class and instantiate a list of {@link Directive} from the recipe.
   *
   * @return List of {@link Directive}.
   */
  @Override
  public List<Directive> parse() throws RecipeException {
    AtomicInteger directiveIndex = new AtomicInteger();
    try {
      List<Directive> result = new ArrayList<>();

      new GrammarWalker(new RecipeCompiler(), context).walk(recipe, (command, tokenGroup) -> {
        directiveIndex.getAndIncrement();
        DirectiveInfo info = registry.get(namespace, command);
        if (info == null) {
          throw new DirectiveNotFoundException(
            String.format("Directive '%s' not found in system and user scope. Check the name of directive.", command)
          );
        }

        try {
          // Support custom token processing
          TokenGroup processedGroup = processTokenGroup(tokenGroup);

          Directive directive = info.instance();
          UsageDefinition definition = directive.define();
          Arguments arguments = new MapArguments(definition, processedGroup);
          directive.initialize(arguments);
          result.add(directive);

        } catch (IllegalAccessException | InstantiationException e) {
          throw new DirectiveLoadException(e.getMessage(), e);
        }
      });

      return result;
    } catch (DirectiveLoadException | DirectiveNotFoundException | DirectiveParseException e) {
      throw new RecipeException(e.getMessage(), e, directiveIndex.get());
    } catch (Exception e) {
      throw new RecipeException(e.getMessage(), e);
    }
  }

  private TokenGroup processTokenGroup(TokenGroup original) {
    List<Token> newTokens = new ArrayList<>();
    for (Token token : original.getTokens()) {
      Token converted = convertIfSpecialToken(token);
      newTokens.add(converted);
    }

    TokenGroup newTokenGroup = new TokenGroup();
    for (Token token : newTokens) {
      newTokenGroup.add(token);
    }
    return newTokenGroup;
  }

  private Token convertIfSpecialToken(Token token) {
    String text = (String) token.value();
    if (isByteSize(text)) {
      return new ByteSize(text);
    } else if (isTimeDuration(text)) {
      return new TimeDuration(text);
    }
    return token;
  }

  private boolean isByteSize(String text) {
    return text.matches("(?i)^\\d+(B|KB|MB|GB|TB|PB)$");
  }

  private boolean isTimeDuration(String text) {
    return text.matches("(?i)^\\d+(ms|s|m|h|d)$");
  }

  /**
   * Utility method for testing — parses a single directive line and returns the token list.
   */
  public List<Token> parseTokens(String directive) throws Exception {
    TokenGroup[] groupHolder = new TokenGroup[1];

    new GrammarWalker(new RecipeCompiler(), context).walk(directive, (command, tokenGroup) -> {
      TokenGroup processed = processTokenGroup(tokenGroup);
      groupHolder[0] = new TokenGroup();

      Text directiveNameToken = new Text(command);
      directiveNameToken.setType(TokenType.DIRECTIVE_NAME); 
      groupHolder[0].add(directiveNameToken);

      for (Token token : processed.getTokens()) {
        groupHolder[0].add(token);
      }
    });

    return groupHolder[0].getTokens();
  }
}
