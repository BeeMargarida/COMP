.class public array2
.super java/lang/Object

.method public static sum_array([I)I
.limit locals 3
.limit stack 1
iconst_0
istore_1


iconst_0
istore_2


loop0:

iload_1
aload_0
arraylength
if_icmpge loop0_end

iload_2
aload_0
iload_1
iaload
iadd
istore_2


iinc 1 1
goto loop0

loop0_end:


iload_2
ireturn
.end method


.method public static main([Ljava/lang/String;)V
.limit locals 4
.limit stack 2
bipush 16
istore_1


iload_1
newarray int
astore_2

iconst_0
istore_3


loop0:

iload_3
iload_1
if_icmpge loop0_end

aload_2
iload_3
iconst_1
iastore
iinc 3 1
goto loop0

loop0_end:

aload_2
invokestatic array2/sum_array([I)I

istore_4


ldc "sum of array elements = "
iload 4
invokestatic io/println(Ljava/lang/String;I)V


return
.end method


.method static public <clinit>()V
.limit stack 0
.limit locals 0

return
.end method
