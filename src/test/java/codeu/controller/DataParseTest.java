package codeu.controller;

import org.junit.Test;
import java.io.IOException;

public class DataParseTest {
  @Test
  public void testParseSample() throws IOException {
    DataParse dp = new DataParse("Sample");
//    dp.parse();
//    System.out.println(dp.allUsers.keySet());
  }

  @Test
  public void testParseTest() throws IOException {
    DataParse dp = new DataParse("Romeo and Juliet");
//    dp.parse();
//    System.out.println(dp.allUsers.keySet());
  }
}
