package thingxII.pokemonswarming.mixin;

import com.pixelmonmod.pixelmon.api.spawning.conditions.SpawnCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.ArrayList;

@Mixin(value = SpawnCondition.class, remap = false)
public interface Patch_WeatherAccessor {
    @Accessor("weathers")
    public void setWeathers(ArrayList<String> weathers);
}
