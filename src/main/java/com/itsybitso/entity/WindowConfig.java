package com.itsybitso.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.itsybitso.util.PropertiesUtil;
import com.itsybitso.util.RandomString;

public class WindowConfig {
  private String id;
  private String ups;
  private String downs;

  public WindowConfig(String upsIn, String downsIn) {
    id = new RandomString().getRandomString();
    if (upsIn == null) {
      this.ups = PropertiesUtil.getProperty("trade.up_m");
    } else {
      this.ups = upsIn;
    }
    if (downsIn == null) {
      this.downs = PropertiesUtil.getProperty("trade.down_n");
    } else {
      this.downs = downsIn;
    }
  }

  public WindowConfig(@JsonProperty("id") String id,
                      @JsonProperty("ups") String ups,
                      @JsonProperty("downs") String downs) {
    this.id = id;
    this.ups = ups;
    this.downs = downs;
  }

  public String getId() {
    return id;
  }

  public String getUps() {
    return ups;
  }

  public String getDowns() {
    return downs;
  }

  @Override
  public String toString() {
    return "WindowConfig{" +
        "id='" + id + '\'' +
        ", ups='" + ups + '\'' +
        ", downs='" + downs + '\'' +
        '}';
  }
}
