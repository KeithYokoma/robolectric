package org.robolectric.annotation.processing.validator;

import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleTypeVisitor8;
import javax.tools.Diagnostic.Kind;
import org.robolectric.annotation.processing.RobolectricModel;

/** Validator that checks usages of {@link org.robolectric.annotation.RealObject}. */
public class RealObjectValidator extends FoundOnImplementsValidator {

  public RealObjectValidator(RobolectricModel.Builder modelBuilder, ProcessingEnvironment env) {
    super(modelBuilder, env, "org.robolectric.annotation.RealObject");
  }

  public static String join(List<?> params) {
    StringBuilder result = new StringBuilder();
    boolean comma = false;
    for (Object p : params) {
      if (comma) {
        result.append(',');
      }
      comma = true;
      result.append(p);
    }
    return result.toString();
  }

  TypeVisitor<Void, VariableElement> typeVisitor =
      new SimpleTypeVisitor8<Void, VariableElement>() {
        @Override
        public Void visitDeclared(DeclaredType t, VariableElement v) {
          List<? extends TypeMirror> typeParams = t.getTypeArguments();
          List<? extends TypeParameterElement> parentTypeParams = parent.getTypeParameters();

          if (!parentTypeParams.isEmpty() && typeParams.isEmpty()) {
            messager.printMessage(Kind.ERROR, "@RealObject is missing type parameters", v);
          } else {
            String typeString = join(typeParams);
            String parentString = join(parentTypeParams);
            if (!typeString.equals(parentString)) {
              messager.printMessage(
                  Kind.ERROR,
                  "Parameter type mismatch: expecting <"
                      + parentString
                      + ">, was <"
                      + typeString
                      + '>',
                  v);
            }
          }
          return null;
        }
      };

  TypeElement parent;

  @Override
  public Void visitVariable(VariableElement elem, TypeElement parent) {
    this.parent = parent;
    TypeMirror impClass = helpers.getImplementedClass(imp);
    if (impClass != null) {
      TypeMirror elemType = elem.asType();
      if (!types.isAssignable(impClass, elemType)) {
        error("@RealObject with type <" + elemType + ">; expected <" + impClass + '>');
      }
      typeVisitor.visit(elemType, elem);
    }
    return null;
  }
}
