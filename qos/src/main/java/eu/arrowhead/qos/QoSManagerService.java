/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

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
package eu.arrowhead.qos;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.qos.DeployedSystem;
import eu.arrowhead.common.database.qos.MessageStream;
import eu.arrowhead.common.database.qos.Network;
import eu.arrowhead.common.database.qos.NetworkDevice;
import eu.arrowhead.common.database.qos.ResourceReservation;
import eu.arrowhead.common.exception.DriverNotFoundException;
import eu.arrowhead.common.exception.ReservationException;
import eu.arrowhead.common.messages.QoSReservationCommand;
import eu.arrowhead.common.messages.QoSReservationResponse;
import eu.arrowhead.common.messages.QoSReserve;
import eu.arrowhead.common.messages.QoSVerificationResponse;
import eu.arrowhead.common.messages.QoSVerifierResponse;
import eu.arrowhead.common.messages.QoSVerify;
import eu.arrowhead.qos.algorithms.VerifierAlgorithmFactory;
import eu.arrowhead.qos.drivers.DriversFactory;
import eu.arrowhead.qos.drivers.ReservationResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

final class QoSManagerService {

    private static final Logger log = Logger.getLogger(QoSManagerService.class.getName());
    private static final VerifierAlgorithmFactory algorithmFactory = VerifierAlgorithmFactory.getInstance();
    private static final DriversFactory driverFactory = DriversFactory.getInstance();
    private static final DatabaseManager dm = DatabaseManager.getInstance();
    private static final HashMap<String, Object> restrictionMap = new HashMap<>();

    private QoSManagerService() throws AssertionError {
        throw new AssertionError("QoSManagerService is a non-instantiable class");
    }

    /**
     * Verifies if the requestedQoS is possible on the selected providers.
     *
     * @param message QoSVerify parameters.
     *
     * @return Returns if is possible or not and why.
     */
    static QoSVerificationResponse qosVerify(QoSVerify message) {
        log.info("QoS: Verifying QoS paramteres.");

        NetworkDevice consumerNetworkDevice = getNetworkDeviceFromSystem(message.getConsumer());
        List<ResourceReservation> consumerReservations = getReservationsFromSystem(message.getConsumer());

        QoSVerificationResponse qosVerificationResponse = new QoSVerificationResponse();
        for (ArrowheadSystem provider : message.getProviders()) {
            NetworkDevice providerNetworkDevice = getNetworkDeviceFromSystem(provider);
            if (providerNetworkDevice == null) {
                continue;
            }
            List<ResourceReservation> providerReservations = getReservationsFromSystem(provider);

            Network network = providerNetworkDevice.getNetwork();
            if (network == null) {
                continue;
            }

            try {
                QoSVerifierResponse response = algorithmFactory
                        .verify(network.getNetworkType(), providerNetworkDevice.getNetworkCapabilities(), consumerNetworkDevice.getNetworkCapabilities(),
                                providerReservations, consumerReservations, message.getRequestedQoS(), message.getCommands());

                qosVerificationResponse.addResponse(provider, response.getResponse());
                if (!response.getResponse()) {
                    qosVerificationResponse.addRejectMotivation(provider, response.getRejectMotivation());
                }
            } catch (Exception ex) {
                log.error(ex.getClass() + " in QosManagerService:qosVerify");
                ex.printStackTrace();
            }
        }

        log.info("QoS: QoS paramteres verified.");
        return qosVerificationResponse;
    }
    // request in a blocking for loop

    private static NetworkDevice getNetworkDeviceFromSystem(ArrowheadSystem system) {
        restrictionMap.clear();
        restrictionMap.put("systemName", system.getSystemName());
        ArrowheadSystem retrievedSystem = dm.get(ArrowheadSystem.class, restrictionMap);

        restrictionMap.clear();
        restrictionMap.put("system", retrievedSystem);
        DeployedSystem deployedSystem = dm.get(DeployedSystem.class, restrictionMap);

        return deployedSystem.getNetworkDevice();
    }

    private static List<ResourceReservation> getReservationsFromSystem(ArrowheadSystem system) {
        restrictionMap.clear();
        restrictionMap.put("systemName", system.getSystemName());
        ArrowheadSystem retrievedSystem = dm.get(ArrowheadSystem.class, restrictionMap);

        //limiting the filtering to just consumers/providers might be desired (need an extra boolean for argument for that)
        restrictionMap.clear();
        restrictionMap.put("consumer", retrievedSystem);
        restrictionMap.put("provider", retrievedSystem);
        List<ResourceReservation> reservations = dm.getAllOfEither(ResourceReservation.class, restrictionMap);

        return reservations;
    }

    /**
     * Reserves a QoS on the consumer and provider stream.
     *
     * @param message QoSReservation parameters.
     *
     * @return Returns if the reservation was possible.
     *
     * @throws ReservationException The reservation on the devices was not
     * possible.
     * @throws DriverNotFoundException The network type doesnt have a driver
     * assigned.
     */
    static QoSReservationResponse qosReserve(QoSReserve message) throws ReservationException, DriverNotFoundException {
        ArrowheadSystem consumer = message.getConsumer();
        ArrowheadSystem provider = message.getProvider();

        NetworkDevice consumerNetworkDevice = getNetworkDeviceFromSystem(consumer);
        NetworkDevice providerNetworkDevice = getNetworkDeviceFromSystem(provider);
        if (providerNetworkDevice == null) {
            throw new ReservationException("");
        }

        Network network = providerNetworkDevice.getNetwork();
        if (network == null) {
            return new QoSReservationResponse(false);
        }

        //Generate commands
        Map<String, String> commands = new HashMap<>();
        ReservationResponse rr;
        try {
            rr = driverFactory.generateCommands(network.getNetworkType(), network.getNetworkConfigurations(), provider, consumer, message.getService(),
                    message.getCommands(), message.getRequestedQoS());
            commands = rr.getNetworkConfiguration();

            ResourceReservation reservation = new ResourceReservation("UP", consumer, provider, message.getRequestedQoS());
            Map<String, Object> restrictionMap = new HashMap<>();

            restrictionMap.put("consumer", consumer);
            restrictionMap.put("provider", provider);

            ResourceReservation current = dm.get(ResourceReservation.class, restrictionMap);

            if (current == null) {
                dm.save(reservation);
            } else {
                current.getQosParameters().putAll(reservation.getQosParameters());
                current.setState(reservation.getState());
                dm.delete(current);
                dm.save(current);
            }

            return new QoSReservationResponse(rr.getSuccess(),
                    new QoSReservationCommand(message.getService(), message.getConsumer(), message.getProvider(), commands,
                            message.getRequestedQoS()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new QoSReservationResponse(false,
                new QoSReservationCommand(message.getService(), message.getConsumer(), message.getProvider(), commands,
                        message.getRequestedQoS()));
    }

    public static boolean qosRelease(ArrowheadSystem consumer) {
        if (consumer == null) {
            return false;
        }

        Map<String, Object> restrictionMap = new HashMap();
        restrictionMap.put("consumer", consumer);

        List<ResourceReservation> rr = dm.getAll(ResourceReservation.class, restrictionMap);

        for (ResourceReservation r : rr) {
            dm.delete(r);
            r.setState("DOWN");
            dm.save(r);
        }

        return true;
    }

}
