/*******************************************************************************
 * Open Source Initiative OSI - The MIT License:Licensing
 * The MIT License
 * Copyright (c) 2010 Charles Wagner Jr. (spiegalpwns@gmail.com)
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package simpleserver.threads;

import java.util.Iterator;

import simpleserver.Player;
import simpleserver.Server;

public class PlayerScanner implements Runnable {
  private Server server;

  public PlayerScanner(Server server) {
    this.server = server;
  }

  public void run() {
    while (true) {
      try {
        Thread.sleep(10000);
      }
      catch (InterruptedException e) {
      }
      synchronized (server.playerList) {
        for (Iterator<Player> itr = server.playerList.iterator(); itr.hasNext();) {
          Player i = itr.next();
          boolean timedOut = i.testTimeout();
          if (timedOut) {
            itr.remove();
            if (!i.isRobot()) {
              System.out.println("[SimpleServer] Disconnecting "
                  + i.getIPAddress() + " due to inactivity.");
            }
            // This is required to make it not try to call notifyClosed()
            i.setKicked(true);
            i.close();
          }
          try {
            Thread.sleep(100);
          }
          catch (InterruptedException e) {
          }
        }
      }
    }
  }
}
