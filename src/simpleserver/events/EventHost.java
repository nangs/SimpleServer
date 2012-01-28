/*
 * Copyright (c) 2010 SimpleServer authors (see CONTRIBUTORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package simpleserver.events;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import simpleserver.Player;
import simpleserver.Server;
import simpleserver.config.xml.Event;

public class EventHost {

  public ConcurrentHashMap<Event, Long> events; // stores last calls for events
  public HashMap<String, String> colors;

  protected Server server;
  protected Random rng;

  protected HashMap<String, RunningEvent> running;

  public void loadEvents() {
    // make old threads shut down if any
    if (running != null) {
      for (String k : running.keySet()) {
        running.get(k).stopping = true;
        running.get(k).interrupt();
      }
    }

    // initialize
    running = new HashMap<String, RunningEvent>();
    events = new ConcurrentHashMap<Event, Long>();

    Iterator<Event> it = server.config.events.iterator();
    while (it.hasNext()) {
      Event ev = it.next();
      events.put(ev, (long) 0);
    }
  }

  public EventHost(Server s) {
    server = s;
    rng = new Random();

    colors = new HashMap<String, String>();
    colors.put("BLACK", "0");
    colors.put("DARK_BLUE", "1");
    colors.put("DARK_GREEN", "2");
    colors.put("DARK_CYAN", "3");
    colors.put("DARK_RED", "4");
    colors.put("PURPLE", "5");
    colors.put("GOLD", "6");
    colors.put("GRAY", "7");
    colors.put("DARK_GRAY", "8");
    colors.put("BLUE", "9");
    colors.put("GREEN", "a");
    colors.put("CYAN", "b");
    colors.put("RED", "c");
    colors.put("PINK", "d");
    colors.put("YELLOW", "e");
    colors.put("WHITE", "f");

    loadEvents();

    Event autorun = findEvent("onServerStart");
    if (autorun != null) {
      execute(autorun, null, true);
    }
  }

  /* Execute given event triggered by given player */
  public void execute(Event e, Player p, boolean forced) {
    // DEBUG
    // System.out.println(p.getName()
    // +"->"+e.name+"@"+System.currentTimeMillis());

    if (e == null) { // no event given -> abort
      return;
    }

    // If it's a player-triggered event, check trigger conditions
    if (!forced && p != null) {
      if (e.disabled || e.coordinate == null)
      {
        return; // Not triggerable
      }

      if (e.allow != null && !e.allow.contains(p))
      {
        return; // user not allowed for event
      }

      long currtime = System.currentTimeMillis();
      if (e.interval != 0 && currtime < events.get(e) + 1000 * e.interval)
      {
        return; // Event still has timeout
      }
      events.put(e, currtime); // Update last event call time
    }

    // Start top level event in new thread
    String threadname = e.name + String.valueOf(System.currentTimeMillis());
    RunningEvent rev = new RunningEvent(this, threadname, e, p);
    rev.start();
    running.put(threadname, rev);
  }

  public Event findEvent(String name) {
    for (Event f : events.keySet()) {
      if (f.name.equals(name)) {
        return f;
      }
    }

    return null;
  }
}