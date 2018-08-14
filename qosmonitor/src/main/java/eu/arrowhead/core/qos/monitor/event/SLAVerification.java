/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.core.qos.monitor.event;

/**
 *
 * @author Renato
 */
public class SLAVerification implements Runnable {

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * private final IProtocol monitor; private final MonitorRule rule; private
     * final MonitorLog log; private static final Logger LOG =
     * Logger.getLogger(SLAVerification.class.getName());
     *
     * public SLAVerification(IProtocol monitor, MonitorRule rule, MonitorLog
     * log) { this.monitor = monitor; this.rule = rule; this.log = log; }
     *
     * @Override public void run() { SLAVerificationResponse SLAresponse; if
     * (rule.isSoftRealTime()) { MonitorLog[] logs =
     * MongoDatabaseManager.getInstance().getLastNLogs(rule); if (logs == null)
     * { return; } SLAresponse = monitor.verifyQoS(rule, logs); } else {
     * SLAresponse = monitor.verifyQoS(rule, log); }
     *
     * if (SLAresponse.isSLABroken()) { // EventProducer eventProducer = new
     * EventProducer(EventUtil.createEvent(SLAresponse.getParameters())); //
     * eventProducer.publishEvent();
     *
     * String queueKey = rule.getProviderSystemName() +
     * rule.getConsumerSystemName(); //
     * monitor.addEventToPresentationQueue(queueKey, eventProducer.getEvent());
     * //FIXME used when not using event handler. testing if
     * (QoSMonitorService.SHOW_GRAPHS) {
     * monitor.addEventToPresentationQueue(queueKey, new
     * PresentationEvent(EventUtil.createEvent(SLAresponse.getParameters()))); }
     *
     * //Only for test purposes
     * SLAresponse.getParameters().stream().forEach((parameter) -> {
     * LOG.info(String.format("Parameter: {0}" + "\n\t" + "Requested Value: {1}"
     * + "\n\t" + "Logged Value: {2}", new Object[]{parameter.getName(),
     * parameter.getRequestedValue(), parameter.getLoggedValue()})); });
     * LOG.warn(String.format("SLA was broken")); } else {
     * LOG.info(String.format("SLA was met")); }
     *
     */
}
