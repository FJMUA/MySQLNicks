package co.ignitus.mysqlnicks;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Pair<K, V> {

    private final K k;

    private final V v;

}
