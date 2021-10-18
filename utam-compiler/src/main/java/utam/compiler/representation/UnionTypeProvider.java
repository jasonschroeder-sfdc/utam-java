package utam.compiler.representation;

import static utam.core.framework.consumer.PageObjectContextImpl.getDefaultImplType;

import java.util.List;
import java.util.stream.Collectors;
import utam.compiler.helpers.TypeUtilities.FromString;
import utam.core.declarative.representation.TypeProvider;
import utam.core.declarative.representation.UnionType;

/**
 * @author elizaveta.ivanova
 * @since
 */
public class UnionTypeProvider extends FromString implements UnionType {

  private final List<TypeProvider> extendedTypes;
  private final List<TypeProvider> implementedTypes;

  public UnionTypeProvider(String fullName, List<TypeProvider> extendedTypes) {
    super(fullName);
    this.extendedTypes = extendedTypes;
    this.implementedTypes = extendedTypes.stream()
        .map(type -> getOriginalImplType(type))
        .collect(Collectors.toList());
  }

  static TypeProvider getOriginalImplType(TypeProvider interfaceType) {
    String[] implType = getDefaultImplType(interfaceType.getFullName());
    return new FromString(implType[0], implType[1]);
  }

  public static TypeProvider getTransformedImplType(TypeProvider classType) {
    String transformedName = classType.getFullName() + "_";
    return new FromString(transformedName);
  }

  @Override
  public List<TypeProvider> getExtendedTypes() {
    return extendedTypes;
  }

  @Override
  public List<TypeProvider> getImplementedTypes() {
    return implementedTypes;
  }
}
