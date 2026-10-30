package io.github.julianperge.modular_planters.gametest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.julianperge.modular_planters.ModularPlanters;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MPTestRegistration {
    public static final Identifier STRUCTURE = id("empty");

    // The mod constructor loads this class before any registry is filled, which runs every tests class's register method and static fields, so a tests class must call get() on mod registry objects only inside its test methods.
    private static final Map<String, TestCase> CASES = cases();

    private MPTestRegistration() {
    }

    private static Map<String, TestCase> cases() {
        Map<String, TestCase> cases = new LinkedHashMap<>();
        RecipeTests.register(cases);
        StructureTests.register(cases);
        StorageTests.register(cases);
        DoorwayTests.register(cases);
        AteBridgeTests.register(cases);
        PlanterTests.register(cases);
        PlanterLifecycleTests.register(cases);
        MenuTests.register(cases);
        CoreScreenTests.register(cases);
        return cases;
    }

    public static void register(IEventBus modEventBus) {
        DeferredRegister<MapCodec<? extends GameTestInstance>> instanceTypes =
                DeferredRegister.create(Registries.TEST_INSTANCE_TYPE, ModularPlanters.MOD_ID);
        instanceTypes.register("case", () -> CaseInstance.CODEC);
        instanceTypes.register(modEventBus);
        modEventBus.addListener(MPTestRegistration::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("default"));
        CASES.forEach((name, testCase) -> event.registerTest(id(name),
                new CaseInstance(name, new TestData<>(environment, STRUCTURE, testCase.maxTicks(), 0, true))));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ModularPlanters.MOD_ID, path);
    }

    private static final class CaseInstance extends GameTestInstance {
        static final MapCodec<CaseInstance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("case").forGetter(test -> test.name),
                TestData.CODEC.forGetter(CaseInstance::info)
        ).apply(instance, CaseInstance::new));

        private final String name;

        CaseInstance(String name, TestData<Holder<TestEnvironmentDefinition<?>>> data) {
            super(data);
            this.name = name;
        }

        @Override
        public void run(GameTestHelper helper) {
            CASES.get(name).body().accept(helper);
        }

        @Override
        public MapCodec<CaseInstance> codec() {
            return CODEC;
        }

        @Override
        protected MutableComponent typeDescription() {
            return Component.literal("Modular Planters case");
        }
    }
}
