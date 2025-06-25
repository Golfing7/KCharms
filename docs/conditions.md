# Charm Conditions
Charm conditions allow you to control when a charm effect works.

## Condition Types
* AFFECTED_PLAYERS
* HELD_LENGTH
* IN_REGION
* PERMISSION

## Configuration
```yaml
# This would be taken out of an effects configuration
conditions:
  # Example PERMISSION type
  perm-1:
    # Define the type of condition
    type: PERMISSION
    # The permission the holder must have
    permission: 'some.permission'
  # Example AFFECTED_PLAYERS type
  affected-players-1:
    type: AFFECTED_PLAYERS
    # Only take effect when affecting 5+ players.
    acceptable-range: '5;1000'
  # Example HELD_LENGTH type
  held-length-1:
    type: HELD_LENGTH
    # In ticks, the player must be holding the charm for 200 ticks or more to take effect
    held-length: '200;10000000'
  # Example IN_REGION
  in-region-1:
    type: IN_REGION
    # The defining region
    region:
      region-type: RECTANGLE
      world: some-world
      min-x: -100
      max-x: 100
      min-z: -100
      max-z: 100
```