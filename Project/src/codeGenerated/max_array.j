.class public max_array
.super java/lang/Object

.method public static maxarray([I)I
.limit locals 3
.limit stack 6
aload_0
iconst_0
iaload
istore_1


iconst_1
istore_2


loop0:

iload_2
aload_0
arraylength
if_icmpge loop0_end

iload_1
aload_0
iload_2
iaload
if_icmpge loop1_end

aload_0
iload_2
iaload
istore_1


loop1_end:
iinc 2 1
iadd
goto loop0

loop0_end:

ldc "max: "
iload_1
invokestatic io/print(Ljava/lang/String;I)V


iload_1
ireturn
.end method


.method public static main([Ljava/lang/String;)V
.limit locals 4
.limit stack 4
bipush 10
newarray int
astore_1

iconst_0
istore_2


loop0:

iload_2
bipush 10
if_icmpge loop0_end

aload_1
iload_2
iload_2
iastore
iinc 2 1
iadd
goto loop0

loop0_end:

iload_1
invokestatic max_array/maxarray([I)I

istore 4



return
.end method


.method static public <clinit>()V
.limit stack 0
.limit locals 0

return
.end method
