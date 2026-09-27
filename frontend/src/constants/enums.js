// Mirrors the enums in com.shaker.entity.* - keep in sync with the backend.

export const VISIBILITY = ['PRIVATE', 'UNLISTED', 'PUBLIC']

export const RECIPE_CATEGORY = [
  'UNFORGETTABLES', 'CONTEMPORARY_CLASSIC', 'NEW_ERA', 'BEFORE_DINNER',
  'AFTER_DINNER', 'LONG_DRINK', 'SPARKLING', 'HOT', 'TIKI', 'OTHER'
]

export const GLASS = [
  'COUPE', 'ROCKS', 'HIGHBALL', 'COLLINS', 'MARTINI', 'NICK_AND_NORA',
  'HURRICANE', 'COPPER_MUG', 'FLUTE', 'SNIFTER', 'TIKI_MUG', 'WINE', 'SHOT'
]

export const ICE_STYLE = ['NEAT', 'UP', 'ROCKS_CUBED', 'ROCKS_ONE_BIG_ROCK', 'CRUSHED', 'BLENDED']

export const PREPARATION_METHOD = ['SHAKEN', 'STIRRED', 'BUILT', 'BLENDED', 'MUDDLED', 'LAYERED', 'THROWN']

export const TASTE_NOTE = [
  'SWEET', 'SOUR', 'BITTER', 'TART', 'SPICY', 'HERBAL',
  'SMOKY', 'FRUITY', 'CREAMY', 'DRY', 'REFRESHING', 'STRONG'
]

export const DIFFICULTY = ['EASY', 'MEDIUM', 'HARD']

export const INGREDIENT_ROLE = [
  'BASE_SPIRIT', 'MODIFIER', 'MIXER', 'SWEETENER', 'BITTERS', 'HERBAL_BOTANICAL', 'DAIRY_EGG', 'OTHER'
]

export const MEASUREMENT_UNIT = [
  'OZ', 'ML', 'CL', 'DASH', 'BARSPOON', 'DROP', 'PART', 'PIECE', 'LEAF', 'SPRIG', 'WEDGE', 'PINCH'
]

export const GARNISH_TYPE = [
  'CITRUS_TWIST', 'CITRUS_WHEEL', 'CHERRY', 'OLIVE', 'HERB_SPRIG', 'RIM_SALT', 'RIM_SUGAR', 'UMBRELLA', 'OTHER'
]

export const INGREDIENT_CATEGORY = [
  'SPIRIT', 'LIQUEUR', 'VERMOUTH', 'BITTERS', 'JUICE', 'SYRUP',
  'SODA_MIXER', 'DAIRY_EGG', 'HERB_BOTANICAL', 'FRUIT', 'GARNISH_ITEM', 'OTHER'
]

export const OPACITY = ['CLEAR', 'TRANSLUCENT', 'OPAQUE', 'CARBONATED']

export const GUIDELINE_CONTENT_TYPE = ['ARTICLE', 'VIDEO', 'PRO_TIP', 'COMMUNITY_TIP']

export const GUIDELINE_AUTHOR_TYPE = ['EDITORIAL', 'VERIFIED_PRO', 'COMMUNITY']

export const MODERATION_STATUS = ['PENDING', 'APPROVED', 'REJECTED']

export const ROLE = ['USER', 'ADMIN']

// "Sweet Vermouth" -> "Sweet Vermouth"; "BASE_SPIRIT" -> "Base spirit"
export function titleCase(enumValue) {
  return enumValue
    .toLowerCase()
    .split('_')
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
    .join(' ')
}

// comma-separated text input <-> string[] helpers, used by every admin form with tag fields
export function tagsToText(tags) {
  return (tags || []).join(', ')
}

export function textToTags(text) {
  return (text || '')
    .split(',')
    .map((t) => t.trim())
    .filter(Boolean)
}
