package cn.octopusyan.dmt.translate;

import cn.octopusyan.dmt.model.WordItem;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 延迟翻译对象
 *
 * @author octopus_yan
 */
@Getter
public class DelayWord implements Delayed {
    @Setter
    private TranslateApi api;
    private final WordItem word;
    private long delayTime;

    public DelayWord(WordItem word) {
        this.word = word;
    }

    public void setDelayTime(long time, TimeUnit timeUnit) {
        this.delayTime = System.currentTimeMillis() + (time > 0 ? TimeUnit.MILLISECONDS.convert(time, timeUnit) : 0);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(delayTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return Long.compare(this.delayTime, ((DelayWord) o).delayTime);
    }
}
