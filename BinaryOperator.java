abstract class BinaryOperator
{
  Chu apply(Chu leftArg, Chu rightArg, Context context)
    throws ExecutionException
  {
    return apply(leftArg, rightArg).conform(context);
  }

  abstract Chu apply(Chu leftArg, Chu rightArg)
    throws ExecutionException;
}
