# GameTests

- `assert` does nothing in GameTests because the server JVM runs without `-ea`. Use `helper.fail(...)`.
- `makeMockPlayer(GameType.CREATIVE)` leaves `hasInfiniteMaterials()` false, so set `abilities.instabuild = true` as well.
- `GameTestHelper.assertValueEqual(actual, expected, msg)` prints its values swapped: "Expected X to be A: was B" means the actual value was A.
