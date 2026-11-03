package io.github.julianperge.modular_planters.gametest;

import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTestHelper;

public record TestCase(int maxTicks, Consumer<GameTestHelper> body) {}
