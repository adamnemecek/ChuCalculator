abstract class UnaryOperator
{
  Chu apply(Chu arg, Context context) throws ExecutionException
  {
    return apply(arg).conform(context);
  }

  abstract Chu apply(Chu arg) throws ExecutionException;
}
