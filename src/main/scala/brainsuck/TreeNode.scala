package brainsuck

trait TreeNode[BaseType <: TreeNode[BaseType]] {
  self: BaseType =>

  def children: Seq[BaseType]

  def same(that: BaseType) = (this eq that) || this == that

  protected def sameChildren(otherChildren: Seq[BaseType]) =
    children.size == otherChildren.size && children.zip(otherChildren).forall(x=>x._1==x._2)

  protected def withChildren(otherChildren: Seq[BaseType]): BaseType =
    if (this sameChildren otherChildren) this else makeCopy(otherChildren)

  protected def makeCopy(args: Seq[BaseType]): BaseType

  def transform(rule: PartialFunction[BaseType, BaseType]): BaseType = transformDown(rule)

  def transformDown(rule: PartialFunction[BaseType, BaseType]): BaseType = {
    val selfTransformed = rule.applyOrElse(this, identity[BaseType])
    if (this same selfTransformed) this transformChildrenDown rule
    else selfTransformed transformChildrenDown rule
  }

  private def transformChildrenDown(rule: PartialFunction[BaseType, BaseType]): BaseType =
    this.withChildren(children.map(_ transformDown rule))

  def transformUp(rule: PartialFunction[BaseType, BaseType]): BaseType = {
    val childrenTransformed = transformChildrenUp(rule)
    if (this same childrenTransformed) rule.applyOrElse(this, identity[BaseType])
    else rule.applyOrElse(childrenTransformed, identity[BaseType])
  }

  private def transformChildrenUp(rule: PartialFunction[BaseType, BaseType]): BaseType =
    this.withChildren(children.map(_ transformUp rule))
}

trait LeafNode[BaseType <: TreeNode[BaseType]] extends TreeNode[BaseType] {
  self: BaseType =>

  override def children = Seq.empty[BaseType]

  override def makeCopy(args: Seq[BaseType]) = this
}

trait UnaryNode[BaseType <: TreeNode[BaseType]] extends TreeNode[BaseType] {
  self: BaseType =>

  def child: BaseType

  override def children = Seq(child)
}
