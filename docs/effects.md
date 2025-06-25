# Charm Effects
Charm effects are the fundamental unit of KCharms.
There are many different types of charm effects, each _doing_ something else.
Each effect should be split into its own config under the `/KCharms/effects/` directory.

## Charm Types
* POTION
* POTION_IMMUNITY
* DAMAGE_RESISTANCE
* ATTRIBUTE

## Basic Configuration
For the sake of demonstration, the `POTION` type of charm will be used.
All charm effects share the same basic configuration.
```yaml
# The type is required for every charm effect
type: POTION
# The potion effects given to the player when the charm takes effect
potion-effects:
  - STRENGTH:2

# This section is optional. If included, it will become a ranged effect.
# Ranged effects will affect nearby players, rather than just the user.
ranged:
  # Range in blocks
  range: 16.0
  # The type of players this will affect.
  # SELF, ENEMY, TEAM
  players:
    - SELF
    - TEAM

# This section is optional. If included, it will make this effect 'active'.
# Active effects must be activated by right-clicking.
# Active effects can also use cooldowns.
active:
  # Optional
  use-message:
    action-bar: 'Used the effect'
  # Optional
  cooldown-message:
    action-bar: 'You''re on cooldown'
  # Optional
  off-cooldown-message:
    action-bar: 'Ready to use'
  # The duration in ticks that the effect will be active after use
  duration: 200
  # The duration in ticks that the cooldown will last
  cooldown-length: 600

# This section is optional. Animations are explained more on the animations page.
animations: {}

# This section is optional. Conditions are explained more on the conditions page.
conditions: {}
```

## Potion Immunity Configuration
```yaml
type: POTION_IMMUNITY
immunities:
  # Immune to poison of this level or higher
  POISON: 1
```

## Damage Resistance Configuration
```yaml
type: DAMAGE_RESISTANCE
damage-modifier: 0.95
```

## Attribute Configuration
```yaml
type: ATTRIBUTE
modifiers:
  ATTACK_DAMAGE:
    number: 1
    # Can be ADD_NUMBER, ADD_SCALAR, MULTIPLY_SCALAR_1
    operation: ADD_NUMBER
```