.class public programa2
.super java/lang/Object

.method public static f1([I)[I
.limit locals 4
.limit stack 3
iconst_0
istore_1


aload_0
arraylength
istore_2


iload_2
newarray int
astore_3

loop0:

iload_1
aload_0
arraylength
if_icmpge loop0_end

aload_0
iload_1
iaload
istore_3


iinc 1 1
goto loop0

loop0_end:


iload_3
ireturn
.end method


.method public static f2(I)[I
.limit locals 2
.limit stack 0
iload_0
newarray int
astore_1

iconst_0
istore_2

loop0:


iload_2
getstatic programa2/a [I 
arraylength

if_icmpge loop0_end


getstatic programa2/a [I 
iload_2
iconst_1iastore

iinc 2 1
goto loop0


loop0_end:



iload_1
ireturn
.end method


.method public static main([Ljava/lang/String;)V
.limit locals 5
.limit stack 6
bipush 100
newarray int
astore_1

iconst_1
istore_1


iconst_2
istore_1


bipush 100
newarray int
astore_2

iconst_0
istore_3

loop0:


iload_3
getstatic programa2/a [I 
arraylength

if_icmpge loop0_end


getstatic programa2/a [I 
iload_3
iload_-1
iastore

iinc 3 1
goto loop0


loop0_end:


aload_2
iconst_0
iaload
istore_3


aload_2
bipush 99
iaload
istore 4


ldc "first: "
iload_3
invokestatic io/println(Ljava/lang/String;I)V

ldc "last: "
iload 4
invokestatic io/println(Ljava/lang/String;I)V

iconst_0
istore 5

loop1:


iload 5
getstatic programa2/a [I 
arraylength

if_icmpge loop1_end


getstatic programa2/a [I 
iload 5
iload_-1
iastore

iinc 5 1
goto loop1


loop1_end:


aload_2
iconst_0
iaload
istore_3


aload_2
bipush 99
iaload
istore 4


ldc "first: "
iload_3
invokestatic io/println(Ljava/lang/String;I)V

ldc "last: "
iload 4
invokestatic io/println(Ljava/lang/String;I)V


return
.end method


.method static public <clinit>()V
.limit stack 0
.limit locals 0

return
.end method
