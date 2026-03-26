package org.raoamigos.trackingservice.service;

import org.raoamigos.trackingservice.dto.DeliveryUpdateEvent;

public interface TrackingEventListener {

    public void handleDeliveryUpdate(DeliveryUpdateEvent event);

}
