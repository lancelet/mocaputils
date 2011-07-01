package mocaputils.transforms

/** Transformation from a point to another point. */
trait XForm 
extends Function1[(Double, Double, Double), (Double, Double, Double)]
