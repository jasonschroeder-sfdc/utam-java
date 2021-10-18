/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: MIT
 * For full license text, see the LICENSE file in the repo root
 * or https://opensource.org/licenses/MIT
 */
package utam.compiler.representation;

import static utam.compiler.representation.MethodDeclarationImpl.getParametersDeclarationString;
import static utam.compiler.translator.TranslationUtilities.getElementGetterMethodName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import utam.compiler.helpers.ElementContext;
import utam.compiler.helpers.ParameterUtils.Regular;
import utam.compiler.helpers.PrimitiveType;
import utam.core.declarative.representation.MethodDeclaration;
import utam.core.declarative.representation.MethodParameter;
import utam.core.declarative.representation.PageObjectMethod;
import utam.core.declarative.representation.TypeProvider;
import utam.core.declarative.representation.UnionType;

/**
 * generate code of getter method for custom element
 *
 * @author elizaveta.ivanova
 * @since 236
 */
public class OneOfMethod implements PageObjectMethod {

  private final static String PARAMETER_NAME = "strParameter";
  private final static List<MethodParameter> parameters = Collections.singletonList( new Regular(PARAMETER_NAME,
      PrimitiveType.STRING));
  private final String methodName;
  private final TypeProvider returnType;
  private final List<TypeProvider> classImports = new ArrayList<>();
  private final List<String> codeLines = new ArrayList<>();

  public OneOfMethod(
      Map<String, ElementContext> elements,
      String elementName,
      UnionType returnType) {
    this.methodName = getElementGetterMethodName(elementName, true);
    this.returnType = returnType;
    this.classImports.add(returnType);
    setCodeLines(elements);
  }

  private void setCodeLines(Map<String, ElementContext> candidates) {
    candidates.forEach( (key, element) -> {
      codeLines.add(String.format(" if(\"%s\".equals(%s)) { return %s(); } ", key, PARAMETER_NAME, element.getElementGetterName()));
    });
    codeLines.add("throw new IllegalArgumentException(String.format(\"Unsupported option '%s'!\", " +  PARAMETER_NAME + "))");
  }

  @Override
  public MethodDeclaration getDeclaration() {
    return new MethodDeclaration() {
      @Override
      public String getName() {
        return methodName;
      }

      @Override
      public List<MethodParameter> getParameters() {
        return parameters;
      }

      @Override
      public TypeProvider getReturnType() {
        return returnType;
      }

      @Override
      public List<TypeProvider> getImports() {
        return new ArrayList<>();
      }

      @Override
      public String getCodeLine() {
        String parametersStr = getParametersDeclarationString(parameters);
        return String.format("%s %s(%s)", getReturnType().getSimpleName(), methodName, parametersStr);
      }
    };
  }

  @Override
  public List<String> getCodeLines() {
    return codeLines;
  }

  @Override
  public List<TypeProvider> getClassImports() {
    return classImports;
  }

  @Override
  public boolean isPublic() {
    return true;
  }
}
