import jakarta.persistence.Tuple;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TupleMapper<T> {

  private final Map<String, Method> setterMap;

  Class<T> clazz;

  public TupleMapper(Class<T> clazz) {
    this.clazz = clazz;

    List<Method> methods = List.of(clazz.getDeclaredMethods());
    ArrayList<Method> methodList = new ArrayList<Method>();
    methods.forEach(m -> {
      if (m.getName().startsWith("set") && m.getParameterTypes().length == 1) {
        methodList.add(m);
      }
    });


    setterMap = methodList.stream()
        .collect(Collectors.toMap(
            m -> getFieldNameFromSetterName(m.getName()),
            m -> m
        ));
  }

  public T toPojo(Tuple row) throws Exception {
    T instance = clazz.getDeclaredConstructor().newInstance();

    row.getElements().forEach(e -> {
      var alias = e.getAlias();
      var setter = setterMap.get(alias);
      try {
        setter.invoke(instance, setter.getParameterTypes()[0].cast(row.get(alias)));
      } catch (InvocationTargetException | IllegalAccessException ex) {
        throw new RuntimeException(ex);
      }
    });

    return instance;
  }

  private static String getFieldNameFromSetterName(String setterName) {
    StringBuilder sb = new StringBuilder(setterName);
    sb.delete(0, 3).setCharAt(0, Character.toLowerCase(sb.charAt(0)));
    return sb.toString();
  }
}
