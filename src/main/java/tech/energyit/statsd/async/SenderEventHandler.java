package tech.energyit.statsd.async;

import com.lmax.disruptor.EventHandler;
import tech.energyit.statsd.Sender;

class SenderEventHandler implements EventHandler<SenderEvent> {

    private Sender sender;

    SenderEventHandler(Sender sender) {
        this.sender = sender;
    }

    public void onEvent(SenderEvent event, long sequence, boolean endOfBatch) {
        sender.send(event.getMsg());
    }

}