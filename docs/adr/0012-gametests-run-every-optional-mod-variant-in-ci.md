# GameTests run every optional-mod variant in CI

Both the Build and Release workflows run the GameTests four times: with every dev mod, with `-PnoAe2`, with `-PnoAppliedFlux` and with `-PnoJade`. Without the variants, a crash for players who lack AE2, Applied Flux or Jade would never fail a test, because the dev runtime includes all three. It costs extra minutes per run, and `just check` runs the same four.

Jade was added as the fourth variant on 2026-11-06. Before that, CI skipped it even though the Jade plan required every GameTest to pass with `-PnoJade`.
