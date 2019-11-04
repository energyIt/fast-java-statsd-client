package tech.energyit.statsd.async;

import com.lmax.disruptor.EventFactory;

class SenderEventFactory implements EventFactory<SenderEvent> {
    @Override
    public SenderEvent newInstance() {
        return new SenderEvent();
    }
}
