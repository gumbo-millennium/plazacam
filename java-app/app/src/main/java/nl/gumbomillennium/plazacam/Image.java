package nl.gumbomillennium.plazacam;

import java.nio.ByteBuffer;

public class Image {
  public final String name;
  public final ByteBuffer photo;

  public Image(String name, ByteBuffer photo) {
    this.name = name;
    this.photo = photo;
  }
}
