/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.qos.drivers;

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This work was supported by National Funds through FCT (Portuguese
 * Foundation for Science and Technology) and by the EU ECSEL JU
 * funding, within Arrowhead project, ref. ARTEMIS/0001/2012,
 * JU grant nr. 332987.
 * ISEP, Polytechnic Institute of Porto.
 */
public interface IQoSDriver {

  /**
   * Configures a stream between a provider and a consumer.
   *
   * @param info Necessary information to the driver.
   *
   * @return Returns the stream configuration parameters.
   */
  ReservationResponse reserveQoS(ReservationInfo info);

}
