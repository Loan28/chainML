import tensorflow as tf
import tensorflow_hub as hub
from matplotlib import pyplot as plt
import numpy as np
import sys
from PIL import Image
import sys


args = sys.argv
file_name = args[1]
label_file = args[2]
model = args[3]

module = hub.KerasLayer("efficientnet/")

im = tf.io.read_file(file_name)
im = tf.image.decode_jpeg(im, channels=3) #color images
im = tf.image.convert_image_dtype(im, tf.float32)
#convert unit8 tensor to floats in the [0,1]range
t = tf.image.resize(im, [224, 224])

images = np.array([t])
images = tf.cast(images, tf.float32)
images = images/255

logits = module(images)  # Logits with shape [batch_size, 1000].
probabilities = tf.nn.softmax(logits)

print(np.argmax(probabilities))
