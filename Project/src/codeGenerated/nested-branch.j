.class public nestedBranch
.super java/lang/Object

.method public static sign(I)I
.limit locals 2
.limit stack 3
iload_0
iconst_0
if_icmpge loop0_end

iconst_m1
istore_1


goto loop0_next
loop0_end:
iload_0
iconst_0
if_icmpne loop1_end

iconst_0
istore_1


goto loop1_next
loop1_end:
iconst_1
istore_1


loop1_next:
loop0_next:

iload_1
ireturn
.end method


.method public static main([Ljava/lang/String;)V
.limit locals 7
.limit stack 3
bipush -10
istore_1


bipush 10
istore_2


iload_1
iload_2
iadd
istore_3


iload_1
invokestatic nestedBranch/sign(I)I

istore 4


iload_3
invokestatic nestedBranch/sign(I)I

istore 5


iload_2
invokestatic nestedBranch/sign(I)I

istore 6


iload 4
invokestatic io/println(I)V

iload 5
invokestatic io/println(I)V

iload 6
invokestatic io/println(I)V


return
.end method


.method static public <clinit>()V
.limit stack 0
.limit locals 0

return
.end method
