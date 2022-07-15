package nl.gumbomillennium.plazacam.models;

import lombok.extern.slf4j.Slf4j;
import org.opencv.videoio.VideoCapture;

@Slf4j
public class Webcam {
  public final String name;

  public final VideoCapture capture;

  public Webcam(String camera) {
    this.name = camera;

    this.capture = new VideoCapture(camera);
  }
}
