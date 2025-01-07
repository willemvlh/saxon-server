package tv.mediagenix.xslt.transformer;

@FunctionalInterface
public interface Action<T>{
   public T run(); 
}
