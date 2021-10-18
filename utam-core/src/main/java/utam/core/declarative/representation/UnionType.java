package utam.core.declarative.representation;

import java.util.List;

/**
 * @author elizaveta.ivanova
 * @since
 */
public interface UnionType extends TypeProvider {

  List<TypeProvider> getExtendedTypes();

  List<TypeProvider> getImplementedTypes();
}
