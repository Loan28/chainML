FROM python:3

ADD resnet_classification.py /

RUN pip install tensorflow
RUN pip install tensorflow_hub
RUN pip install numpy
RUN pip install matplotlib
RUN pip install Pillow
RUN pip install Image
RUN pip install scikit-image
CMD [ "python", "./resnet_classification.py" ]