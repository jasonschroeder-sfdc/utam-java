package utam.compiler.grammar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import utam.compiler.helpers.ElementContext;
import utam.compiler.helpers.LocatorCodeGeneration;
import utam.compiler.helpers.TranslationContext;
import utam.compiler.helpers.TypeUtilities.FromString;
import utam.compiler.representation.CustomElementMethod;
import utam.compiler.representation.OneOfMethod;
import utam.compiler.representation.UnionTypeProvider;
import utam.core.declarative.representation.PageObjectMethod;
import utam.core.declarative.representation.TypeProvider;
import utam.core.declarative.representation.UnionType;
import utam.core.framework.consumer.PageObjectContextImpl;

/**
 * @author elizaveta.ivanova
 * @since
 */
class UtamElementOneOf {

  final String elementName;
  final UtamSelector selector;
  final Map<String, OneOfCandidates> values = new HashMap<>();

  UtamElementOneOf(String elementName, JsonNode oneOf, UtamSelector selector) {
    this.elementName = elementName;
    this.selector = selector;
    oneOf.fieldNames().forEachRemaining(key -> {
      JsonNode element = oneOf.get(key);
      try {
        OneOfCandidates oneOfCandidates = new ObjectMapper()
            .treeToValue(element, OneOfCandidates.class);
        values.put(key, oneOfCandidates);
      } catch (IOException e) {

      }
    });
  }

  String getTypeName() {
    return elementName.substring(0, 1).toUpperCase()
        + elementName.substring(1);
  }

  static UtamElement.Traversal buildTraversal(JsonNode oneOf, String elementName, UtamSelector selector) {
    if (oneOf == null || !oneOf.isObject()) {
      return null;
    }
    return new OneOfTraversal(new UtamElementOneOf(elementName, oneOf, selector));
  }

  static class OneOfCandidates {

    private final UtamSelector elementSelector;
    private final String type;

    @JsonCreator
    OneOfCandidates(
        @JsonProperty(value = "type", required = true) String type,
        @JsonProperty(value = "selector") UtamSelector selector) {
      this.type = type;
      this.elementSelector = selector;
    }

    private LocatorCodeGeneration getSelector(UtamElementOneOf elementOneOf, TranslationContext context) {
      UtamSelector selector = this.elementSelector == null? elementOneOf.selector : this.elementSelector;
      return selector.getCodeGenerationHelper(context);
    }

    private TypeProvider getElementGetterReturnType(TranslationContext context) {
      String packageName = context.getClassType().getPackageName();
      String originalTypeName = context.getType(type).getFullName();
      String originalTypeClassName = PageObjectContextImpl.getDefaultImplType(originalTypeName)[0];
      String transformedTypeName = String.format("%s.%s", packageName, originalTypeClassName);
      return UnionTypeProvider.getTransformedImplType(new FromString(transformedTypeName));
    }

    TypeProvider getUnionType(TranslationContext context) {
      return context.getType(type);
    }

    ElementContext getElementContext(
        UtamElementOneOf elementOneOf,
        TranslationContext translationContext,
        ElementContext scopeElement,
        boolean isExpandScopeShadowRoot,
        String key,
        String elementName) {
      String name = String.format("%s_%s", elementName, key);
      LocatorCodeGeneration selectorContext = getSelector(elementOneOf, translationContext);
      TypeProvider elementType = getElementGetterReturnType(translationContext);
      ElementContext elementContext = new ElementContext.Custom(
          scopeElement,
          name,
          elementType,
          selectorContext.getLocator(),
          new ArrayList<>(),
          false);
      CustomElementMethod.Root root = new CustomElementMethod.Root(selectorContext);
      PageObjectMethod method =
          new CustomElementMethod.Single(
              false,
              name,
              root, scopeElement, false, elementType, false, isExpandScopeShadowRoot);

      elementContext.setElementMethod(method);
      translationContext.setMethod(method);
      translationContext.setMethodUsage(method.getDeclaration().getName());
      return elementContext;
    }
  }

  static class OneOfTraversal extends UtamElement.Traversal {

    private final UtamElementOneOf utamElement;

    OneOfTraversal(UtamElementOneOf utamElement) {
      this.utamElement = utamElement;
    }

    @Override
    ElementContext[] traverse(TranslationContext context, ElementContext scopeElement,
        boolean isExpandScopeShadowRoot) {
      Map<String, ElementContext> elements = new HashMap<>();
      List<TypeProvider> unionTypes = new ArrayList<>();
      utamElement.values
          .entrySet()
          .stream()
          .forEach(entry -> {
            String key = entry.getKey();
            OneOfCandidates value = entry.getValue();
            ElementContext elementContext =
                value.getElementContext(
                    utamElement,
                    context,
                    scopeElement,
                    isExpandScopeShadowRoot,
                    key,
                    utamElement.elementName);
            elements.put(key, elementContext);
            unionTypes.add(value.getUnionType(context));
          });
      String typeName = String.format("%s.%s", context.getSelfType().getPackageName(), utamElement.getTypeName());
      UnionType unionType = new UnionTypeProvider(typeName, unionTypes);
      context.setUnionType(unionType);
      PageObjectMethod method = new OneOfMethod(elements,
          utamElement.elementName, unionType);
      context.setMethod(method);
      return new ElementContext[] { null };
    }
  }
}
