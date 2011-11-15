/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS,  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.powertac.common.interfaces;

/**
 * Visualization Proxy is responsible for forwarding selected server data
 * to any registered visualizer
 *
 * @author David Dauer
 */
public interface VisualizerProxy {

  /**
   * Sets up a dispatch listener for market messages
   */
  void registerVisualizerMessageListener(VisualizerMessageListener listener);

  /**
   * Forwards message to registered listeners
   */
  void forwardMessage(Object message);

}
