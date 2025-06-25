# Charm Animations
Charm animations are visual/sound fx that will appear when a charm effect works.

## Animation Types
* MESSAGE
* GLOW
* TEXT_DISPLAY
* BLOCK_DISPLAY
* ITEM_DISPLAY

## Message Configuration
```yaml
animations:
  message-animation:
    type: MESSAGE
    # Sent when the charm effect is first applied
    on-start: 'The effect is starting'
    # Sent every second to all players that are affected
    on-tick:
      action-bar: 'The effect is ticking'
    # Sent when the effect is no longer taking effect
    on-end: 'The effect has ended'
```

## Glow Configuration
There is no specific configuration that takes place here. It simply applies the glowing effect to players. 
```yaml
animations:
  glow-animation:
    type: GLOW
```

## Display Configuration
All display type animations share common configuration
```yaml
animations:
  display-animation:
    type: ... # Some display type
    # A scale vector, optional
    scale: "1:1:1"
    # A vector for translation (movement), optional
    translation: "0:0:0"
    # Left rotation of the transformation, optional
    left-rotation:
      # Axis of rotation
      vector: "0:1:0"
      # The angle in degrees
      angle: 90
    # Right rotation of the transformation, optional
    right-rotation:
      # Axis of rotation
      vector: "0:1:0"
      angle: 90
    # The billboard of the display entity, optional
    # The billboard is how a display entity should face the player as the player moves around it.
    # FIXED, VERTICAL, HORIZONTAL, CENTER
    billboard: FIXED
```
There are some more technical options you can configure with. For the sake of simplicity, they're left out.
See [this file](../src/main/java/com/golfing8/kcharm/module/animation/CharmAnimationDisplay.java) for all configuration options.

## Text Display Animation
This uses the new Text Display entities added in modern versions of MC.
```yaml
animations:
  td-animation:
    type: TEXT_DISPLAY
    # The text to display, minimessage supported
    text: 'Some Text Line'
    # The width of the text line, optional
    line-width: 200
    # The background color of the text, optional
    bg-color: '000000'
    # The opacity of the display, optional
    opacity: -1
    # Whether the text is shadowed, optional
    shadowed: false
    # Whether the text is see-through, optional
    see-through: false
    # Whether to use the default background, optional
    default-bg: false
    # The text alignment, optional
    # LEFT, CENTER, RIGHT
    alignment: CENTER
```

## Block Display Animation
This uses the new Block Display entities added in modern versions of MC.
```yaml
animations:
  bd-animation:
    type: BLOCK_DISPLAY
    # The block type
    material: DIAMOND_BLOCK
```

## Item Display Animation
This uses the new Item Display entities added in modern versions of MC.
```yaml
animations:
  item-animation:
    type: ITEM_DISPLAY
    # The item to show
    item:
      type: DIAMOND_BLOCK
    # The display transformation, optional
    # NONE, THIRDPERSON_LEFTHAND, THIRDPERSON_RIGHTHAND, FIRSTPERSON_LEFTHAND, FIRSTPERSON_RIGHTHAND, HEAD, GUI, GROUND, FIXED
    item-display-transform: NONE
```
