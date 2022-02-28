/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: MIT
 * For full license text, see the LICENSE file in the repo root
 * or https://opensource.org/licenses/MIT
 */
package utam.compiler.representation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.testng.annotations.Test;
import utam.compiler.helpers.ParameterUtils.Regular;
import utam.compiler.helpers.PrimitiveType;
import utam.compiler.helpers.TypeUtilities;
import utam.core.declarative.representation.MethodParameter;
import utam.core.declarative.representation.TypeProvider;

/**
 * Provides tests for the MethodDeclaration class
 *
 * @author james.evans
 */
public class MethodDeclarationImplTests {

  /** A MethodDeclaration object should be able to be constructed without parameters */
  @Test
  public void testMethodDeclarationCreation() {
    TypeProvider returnType = new TypeUtilities.FromString("ReturnType", "test.ReturnType");

    MethodDeclarationImpl declaration =
        new MethodDeclarationImpl(
            "fakeMethod",
            new ArrayList<>(),
            returnType);
    assertThat(declaration.getName(), is(equalTo("fakeMethod")));
    assertThat(declaration.getReturnType(), is(equalTo(returnType)));
    assertThat(declaration.getParameters(), hasSize(0));
    assertThat(declaration.getImports(), is(equalTo(Collections.singletonList(returnType))));
  }

  /** A MethodDeclaration object should be able to be constructed with parameters */
  @Test
  public void testMethodDeclarationCreationWithParameters() {
    MethodParameter param1 = new Regular("param1", PrimitiveType.STRING);
    TypeProvider returnType = new TypeUtilities.FromString("ReturnType", "test.ReturnType");
    List<MethodParameter> parameters = Collections.singletonList(param1);

    MethodDeclarationImpl declaration =
        new MethodDeclarationImpl(
            "fakeMethod",
            parameters,
            returnType);
    assertThat(declaration.getName(), is(equalTo("fakeMethod")));
    assertThat(declaration.getReturnType(), is(equalTo(returnType)));
    assertThat(declaration.getParameters(), is(equalTo(parameters)));
    assertThat(declaration.getImports(), is(equalTo(Collections.singletonList(returnType))));
    assertThat(declaration.getCodeLine(), is(equalTo("ReturnType fakeMethod(String param1)")));
  }

  /** A MethodDeclaration object should be able to be constructed with parameters and imports */
  @Test
  public void testMethodDeclarationCreationWithParametersAndImports() {
    MethodParameter param1 = new Regular("param1", PrimitiveType.STRING);
    MethodParameter param2 = new Regular("param2", PrimitiveType.STRING);

    TypeProvider returnType = new TypeUtilities.FromString("ReturnType", "test.ReturnType");
    List<MethodParameter> parameters = Arrays.asList(param1, param2);

    MethodDeclarationImpl declaration =
        new MethodDeclarationImpl(
            "fakeMethod",
            parameters,
            returnType);
    assertThat(declaration.getName(), is(equalTo("fakeMethod")));
    assertThat(declaration.getReturnType(), is(equalTo(returnType)));
    assertThat(declaration.getParameters(), is(equalTo(parameters)));
    assertThat(declaration.getImports(), is(containsInAnyOrder(returnType)));
    assertThat(
        declaration.getCodeLine(),
        is(equalTo("ReturnType fakeMethod(String param1, String param2)")));
  }
}
