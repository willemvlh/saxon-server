package tv.mediagenix.xslt.transformer.saxon.core;

import java.io.IOException;
import java.io.OutputStream;

public class CancelableOutputStream extends OutputStream{
  private volatile boolean _cancelled = false;
  private final OutputStream _delegate;
  public CancelableOutputStream(OutputStream delegate) {
   _delegate = delegate; 
  }

  public void cancel(){
    _cancelled = true;
  }

  @Override
  public void write(int b) throws IOException {
    if(_cancelled){
      throw new TransformationCanceledException();
    }
    _delegate.write(b);
  }
}
class TransformationCanceledException extends IOException {}
