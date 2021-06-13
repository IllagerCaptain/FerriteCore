package malte0811.ferritecore.impl;

import malte0811.ferritecore.classloading.FastImmutableMapDefiner;
import malte0811.ferritecore.ducks.FastMapStateHolder;
import malte0811.ferritecore.fastmap.FastMap;
import malte0811.ferritecore.fastmap.table.CrashNeighborTable;
import malte0811.ferritecore.fastmap.table.FastmapNeighborTable;
import malte0811.ferritecore.mixin.config.FerriteConfig;
import net.minecraft.state.Property;

import java.util.Map;

public class StateHolderImpl {
    public static final ThreadLocal<Map<Map<Property<?>, Comparable<?>>, ?>> LAST_STATE_MAP = new ThreadLocal<>();
    public static final ThreadLocal<FastMap<?>> LAST_FAST_STATE_MAP = new ThreadLocal<>();

    /**
     * Set up the {@link FastMap} used by the given {@link FastMapStateHolder} to handle neighbors and property lookups.
     * This is called in a loop for each {@link net.minecraft.state.StateContainer}, so all state holders of a given
     * container will use the same {@link FastMap} instance.
     */
    public static <S>
    void populateNeighbors(Map<Map<Property<?>, Comparable<?>>, S> states, FastMapStateHolder<S> holder) {
        if (holder.getNeighborTable() != null) {
            throw new IllegalStateException();
        } else if (states == LAST_STATE_MAP.get()) {
            // Use threadlocal state to use the same fast map for all states of one block
            holder.setStateMap((FastMap<S>) LAST_FAST_STATE_MAP.get());
        } else {
            LAST_STATE_MAP.set(states);
            FastMap<S> globalTable = new FastMap<>(
                    holder.getVanillaPropertyMap().keySet(), states, FerriteConfig.COMPACT_FAST_MAP.isEnabled()
            );
            holder.setStateMap(globalTable);
            LAST_FAST_STATE_MAP.set(globalTable);
        }
        int index = holder.getStateMap().getIndexOf(holder.getVanillaPropertyMap());
        holder.setStateIndex(index);
        if (FerriteConfig.PROPERTY_MAP.isEnabled()) {
            holder.replacePropertyMap(FastImmutableMapDefiner.makeMap(holder));
        }
        if (FerriteConfig.POPULATE_NEIGHBOR_TABLE.isEnabled()) {
            holder.setNeighborTable(new FastmapNeighborTable<>(holder));
        } else {
            holder.setNeighborTable(CrashNeighborTable.getInstance());
        }
    }
}
