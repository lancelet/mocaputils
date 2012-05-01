package mocaputils.transforms

import mocaputils.Vec3

/** Transformation from a point to another point. */
trait XForm 
extends Function1[Vec3, Vec3]
